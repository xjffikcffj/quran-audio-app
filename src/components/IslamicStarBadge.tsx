interface IslamicStarBadgeProps {
  number: number;
  isCurrent: boolean;
  size?: number;
}

export function IslamicStarBadge({
  number,
  isCurrent,
  size = 44,
}: IslamicStarBadgeProps) {
  const center = size / 2;
  const outerRadius = size / 2 - 2;
  const innerRadius = outerRadius * 0.72;
  const numPoints = 8;

  const points: string[] = [];
  for (let i = 0; i < numPoints * 2; i++) {
    const r = i % 2 === 0 ? outerRadius : innerRadius;
    const angle = (i * Math.PI) / numPoints - Math.PI / 2;
    const x = center + r * Math.cos(angle);
    const y = center + r * Math.sin(angle);
    points.push(`${x.toFixed(2)},${y.toFixed(2)}`);
  }

  return (
    <div
      className="relative flex items-center justify-center shrink-0"
      style={{ width: size, height: size }}
    >
      <svg
        width={size}
        height={size}
        viewBox={`0 0 ${size} ${size}`}
        aria-hidden="true"
      >
        <polygon
          points={points.join(' ')}
          fill={
            isCurrent
              ? 'var(--islamic-gold)'
              : 'var(--islamic-card-hover)'
          }
          stroke={
            isCurrent
              ? 'var(--islamic-gold-light)'
              : 'var(--islamic-border)'
          }
          strokeWidth={1}
        />
      </svg>
      <span
        className="absolute inset-0 flex items-center justify-center font-cairo font-bold tabular-nums"
        style={{
          fontSize: size <= 38 ? '11px' : '13px',
          color: isCurrent
            ? 'var(--islamic-on-gold)'
            : 'var(--islamic-gold-light)',
        }}
      >
        {number}
      </span>
    </div>
  );
}

export function PlayingSoundWave() {
  return (
    <div
      className="flex items-center gap-[3px] h-6 px-1"
      aria-label="قيد التشغيل"
    >
      <span
        className="w-[3px] h-5 rounded-sm animate-wave-1"
        style={{ backgroundColor: 'var(--islamic-gold)' }}
      />
      <span
        className="w-[3px] h-5 rounded-sm animate-wave-2"
        style={{ backgroundColor: 'var(--islamic-gold)' }}
      />
      <span
        className="w-[3px] h-5 rounded-sm animate-wave-3"
        style={{ backgroundColor: 'var(--islamic-gold)' }}
      />
    </div>
  );
}
